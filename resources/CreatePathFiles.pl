#!/usr/bin/perl -w
$|++;
my $VERSION = '1.0.0';
use utf8;
use strict;
use Encode;
use File::Basename 'fileparse';
use Storable qw(retrieve);
use Cwd qw(abs_path getcwd);
use File::Basename;
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

my ($funcDir, $funcName) = (abs_path($0) =~ m{^(.*)/(.*)});
binmode(STDOUT, ':utf8');
binmode(STDIN, ':utf8');
binmode(STDERR, ':utf8');
@ARGV = do {
    @ARGV = map { decode 'UTF8', $_ } @ARGV;
};
my $USAGE = "USAGE: $funcName [-f] [-k|-K|-a] [char]
    -k  : Create only the kana files
    -K  : Create only the Kanji files (default)
    -a  : Create all files
    char: Create a path file for 'char' only.
    Kvg svg files should be under the svg directory under the
    path to this executable.  Path files are put in the current
    directory.
";
die $USAGE, "\nNo arguments.\n" unless @ARGV;
my @args = @ARGV;
my ($RENDER_CHAR, $RENDER_MODE) = (undef, "kanji");
while(defined(my $arg = shift @args)) {
    if ($arg =~ /^-([akK])$/) {
        $RENDER_MODE = ($1 eq "a") ? "all"
            : ($1 eq "k") ? "kana"
                : "kanji";
    }
    else {
        $RENDER_CHAR = $arg;
    }
    last;
}
unless (defined $RENDER_MODE) {
    die $USAGE, "Please use render mofe '-a', '-k', or '-K'.\n";
}

my $svgFilesDir = "$funcDir/kanji";
unless (-d $svgFilesDir) {
    die $USAGE, "Path output directory $svgFilesDir not found.\n"}
my $pathFileDir = "$funcDir/paths";
unless (-d $pathFileDir) {
    die $USAGE, "Path output directory $pathFileDir not found.\n"}
# map char => svg filename.
my %chrRange = (
    hiragana => qr/[\x{3041}-\x{3096}]/,
    katakana => qr/[\x{30A0}-\x{30FF}]/,
    kanji => qr/[\x{3400}-\x{4DB5}\x{4E00}-\x{9FCB}\x{F900}-\x{FA6A}]/,
);
my $ordRegex = qr{^.*/([a-fA-F0-9]{5})};
sub toOrd { ($_[0] =~ $ordRegex) [0] }
sub toChr { chr(hex(toOrd($_[0]))) }
sub ordIsIn {
    my ($name, $range) = @_;
    my $chr = toChr($name);
    return ($range eq "all") ? $chr
        : ($chr =~ $chrRange{$range}) ? $chr
            : undef
}
my @renderFiles = grep defined ordIsIn($_, $RENDER_MODE), <$svgFilesDir/*.svg>;

# <g id="kvg:05132-Hyougai" kvg:element="儲">
my $idRegex = qr{^\s*<g\s+
    id="([^"]+)"
    [^:]+:
    element="([^"]+)"\s*>
}x;
my $pathRegex = qr{^\s*<path\s.*\sd="([^"]+")};
my $widthRegex = qr {
    <svg\s.*width="([^"]+)".*height="([^"]++)
}x;
# It turns out that for the text transform matrix used by
# kanjiVG, the last two values are the x/y location for text
# placement.
my $textRegex = qr{
    ^\s*<text.*matrix\([^)]+   # text starts with "<text transform="
    \s+(\d+(?:\.\d+.)*)        # Followed by the a transform matrix.
    \s+(\d+(?:\.\d+)*)\)[^>]+> # last values in the matrix are x,y
    ([^<]+)                    # and the text.
}x;
sub Get {
    my @curXY;
    my $relToAbs = sub {
        my @xy = $_[0] =~ m{(\d+(?:\.\d+)*)}g;
        my @abs;
        while (@xy) {
            $xy[0] += $curXY[0];
            $xy[1] += $curXY[1];
            push(@abs, shift(@xy));
            push(@abs, shift(@xy));
        }
        # coerce to float.
        @abs = map {
            (/\.\d+/) ? $_ : sprintf('%.1f', $_)
        } @abs;
        my $rv = join(',', @abs);
        # for svg, you don't need a ',' for separation
        # of negative numbers.
        $rv =~ s/,-/-/g;
        return $rv;
    };
    my $toAbsOps = sub {
        my @ops = $_[0] =~ m{([a-zA-Z][^a-zA-Z]+)}g;
        my @rv;
        for my $op (@ops) {
            if ($op =~ /^M/) {
                @curXY = $op =~ m{(-*\d+(?:\.\d+)*)}g;
                push @rv, $op }
            elsif ($op =~ /^c/) {
                my $abs = $relToAbs->($op);
                push(@rv, "C$abs");
            }
            else {
                die "Unhandled path op:$op\n";
            }
        }
        return join('', @rv);
    };
    my @pathInfo;
    my $file = $_[0];
    open(F, $file) || die "Failed to open $file for read:$!\\n";
    my ($width, @paths, @annotations, $fname, $renderChar);
    my $lineNumber = 0;
    $fname = "N" . basename $file;
    while (<F>) {
        chomp;
        $lineNumber++;
        if ($_ =~ $pathRegex) {
            push @paths, $1 }
        elsif ($_ =~ $textRegex) {
            push @annotations, "X$1,$2,$3"}
        elsif ($_ =~ $widthRegex) {
            $width = "W$1,$2" }
        elsif ($_ =~ $idRegex) {
            $renderChar = "C$2" unless(defined $renderChar); }
        else {
            # print "$_\n";
        }
    }
    if ($#paths != $#annotations) {
        die "Expected 1 annotation per path. ",
            scalar @annotations, " != ", scalar @paths, "\n";
    }
    push @pathInfo, $fname, $renderChar, $width;
    # interleave paths and annotation so path annotation
    # is displayed as path is finished.
    foreach my $i (0..$#paths) {
        # push @pathInfo, "S" . $toAbsOps->($paths[$i]);
        push @pathInfo, "S" . $paths[$i];
        push @pathInfo, $annotations[$i];
    }
    return @pathInfo;
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
my $licenseFile = "$pathFileDir/License.txt";
open (OUT, "> $licenseFile") || die "open $licenseFile for output FAILED:$!\n";
binmode(OUT, ':utf8');
print OUT $HEADER;
close OUT;
foreach my $renderFile (@renderFiles) {
    if (-f $renderFile) {
        my ($ord, $other) = $renderFile =~ m{/([0-9a-fA-f]{5})([^.]+)*.svg$};
        my $renderChr = chr(hex($ord));
        my @paths = Get($renderFile);
        my $pathFile = "$pathFileDir/$renderChr" . ($other || '') . ".avg";
        open (OUT2, "> $pathFile") || die "open $pathFile for output FAILED:$!\n";
        binmode(OUT2, ':utf8');
        print OUT2 join("\n", @paths, "");
        printf("$renderChr ");
        close OUT2;
    }
}
print "\n";
close OUT;
exit;
