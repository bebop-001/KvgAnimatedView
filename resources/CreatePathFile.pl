#!/usr/bin/perl -w
$|++;
my $VERSION = '1.0.0';
use utf8;
use strict;
use Encode;
use File::Basename 'fileparse';
use Storable qw(retrieve);
use Cwd qw(abs_path getcwd);
# Catch path and name to executable.  If we're using a symbolic
# link, path goes to real executable but name is of the link.
my ($ExecPath, $ExecName) = (abs_path($0) =~ m{^(.*/)(.*)});
$ExecName = ($0 =~ m{^.*/(.*)})[0] || $0;
my $HEADER = "# Created by $ExecName version $VERSION
# This file was derived from files provided by Ulrich Apel 
# of KanjiVG.  The following header is extracted from the
# header of his files and included to show attribution.
# Copyright (C) 2015 Ulrich Apel.
# This work is distributed under the conditions of the
# Creative Commons Attribution-Share Alike 3.0 Licence.
# This means you are free:
# * to Share - to copy, distribute and transmit the work
# * to Remix - to adapt the work
# Under the following conditions:
# * Attribution. You must attribute the work by stating
#   your use of KanjiVG in your own copyright header and
#   linking to KanjiVG's website (http://kanjivg.tagaini.net)
# * Share Alike. If you alter, transform, or build upon this
# work, you may distribute the resulting work only under the
# same or similar license to this one.
#
# See http://creativecommons.org/licenses/by-sa/3.0/ for more details.
";
# use URL::Encode qw(url_encode_utf8);

binmode(STDOUT, ':utf8');
binmode(STDIN, ':utf8');
binmode(STDERR, ':utf8');
@ARGV = do {
    @ARGV = map { decode 'UTF8', $_ } @ARGV;
};

sub Get {
    my $file = $_[0];
    open(F, $file) || die "Failed to open $file for read:$!\\n";
    my (@paths, $key);
    $key = sprintf("%06x",
        hex(($file =~ m{/([\da-zA-Z]+)\.svg$})[0]));
    my ($width, $height, $h_scale_factor, $v_scale_factor);
    while (<F>) {
        # use width and hight to calculate a scale factor for
        # normalizing char to be 100 x 100 pix
        if (/<svg\s.*width="([^"]+)".*height="([^"]++)/) {
            $width = $1; $height = $2;
            $h_scale_factor = 100 / $width;
            $v_scale_factor = 100 / $height;
        }
        elsif (/<path/) {
            my $p = ($_ =~ /\s+d="([^"]+)/)[0];
            my @p = grep(length, split(/([a-zA-Z])/, $p));
            my ($op, $vals);
            my @pp;
            while (@p && (($op, $vals) = (shift @p, shift @p))) {
                my @vals = map{sprintf("%.3f", $_)}
                    ($vals =~ m{(-*\d+(?:\.\d+)*)}g);
                # apply scale factor to result in 100x100 pix char.
                foreach my $i (0..$#vals) {
                    $_ = $vals[$i];
                    # odd index is y value.
                    if ($i & 01) {
                        $_ *= $h_scale_factor
                    }
                    else {
                        $_ *= $v_scale_factor
                    }
                }
                push @pp, "$op:"
                    . join(',', map {sprintf("%.3f", $_)}@vals);
            }
            push @paths, join(' ', chr(hex($key)), @pp);
        }
    }
    return @paths;
}

my @kana = qw (
    あ い う え お
    か き く け こ
    さ し す せ そ
    た ち つ て と
    な に ぬ ね の
    は ひ ふ へ ほ
    ま み む め も
    や    ゆ    よ
    ら り る れ ろ
    わ    を    ん   
    ア イ ウ エ オ
    カ キ ク ケ コ
    サ シ ス セ ソ
    タ チ ツ テ ト
    ナ ニ ヌ ネ ノ
    ハ ヒ フ ヘ ホ
    マ ミ ム メ モ
    ヤ    ユ    ヨ
    ラ リ ル レ ロ
    ワ          ン
);
my (@missing, @SvgFiles);
my $OutFile = "kanjivg.kana.pat";
my $out2File = "kanjivg.kana.%04x.pat";
open (OUT, "> $OutFile") || die "open $OutFile for output FAILED:$!\n";
binmode(OUT, ':utf8');
print OUT $HEADER;
foreach my $kana (@kana) {
    my $file = sprintf("kanji/%05x.svg", ord($kana));
    my $out2 = sprintf($out2File, ord($kana));
    open (OUT2, "> $out2") || die "open $out2 for output FAILED:$!\n";
    binmode(OUT2, ':utf8');
    print OUT2 $HEADER;
    if (-f $file) {
        my @p = Get($file);
        my $kvgPat = join("\n",
            sprintf("%s %d", $kana, scalar @p),
            @p, '');
        print OUT $kvgPat;
        print OUT2 $kvgPat;
        printf("$kana:%04x ", ord($kana));
        close OUT2;
    }
    else {
        push @missing, "$kana:$file" unless -f $file;
    }
}
print "\n";
close OUT;
die "@missing\n" if @missing;
exit;
