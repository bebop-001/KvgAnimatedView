#!/usr/bin/perl -w
$|++;
my $VERSION = '1.0.0';
use utf8;
use strict;
use Encode;
use Cwd 'abs_path';
my ($funcDir, $funcName) = (abs_path($0) =~ m{^(.*)/(.*)});

binmode(STDOUT, ':utf8');
binmode(STDIN, ':utf8');
binmode(STDERR, ':utf8');
@ARGV = do {
    @ARGV = map { decode 'UTF8', $_ } @ARGV;
};

my $USAGE = "USAGE: $funcName [-f] [-k|-K|-a] [char] svgDir pathsDir
    -k  : Create only the kana files
    -K  : Create only the Kanji files
    -a  : Create all files
    char: Create a path file for 'char' only.
    Kvg svg files should be under the svg directory under the
    path to this executable.  Path files are put in the current
    directory.
";
die $USAGE, "\nNo arguments.\n" unless @ARGV;
die $USAGE, "bad arg:$ARGV[0]\n" if ($ARGV[0] =~ /^-/ && $ARGV[0] !~ /^-[akK]$/);
my @args = @ARGV;
unless (-d $args[-1]) {
    die $USAGE, "Mising output directory arg.\n", "Last directory should be the output directory.\n";
}
my $pathsDir = pop @args;
unless (-d $args[-1]) {
    die $USAGE, "Next to Last directory should be the svg file directory.\n";
}
my $svgDir = pop @args;

my ($RENDER_CHAR, $RENDER_MODE);
# Boolean. true()/false() return "true"/"false". 
# true(val) returns true if val is "true"
# false(val) returns true if val is "false".
# die unless val is "true" or "false".
sub tf {
    unless ($_[0] && $_[0] =~ /^(true|false)$/i) {
        die "true/false:: Expeceted \"true\" or \"false\"\n";
    }
    return (lc($_[0]) eq $_[1]);
}
sub true {
    return "true" if @_ == 0;
    return tf($_[0], "true");
}
sub false {
    return "false" if @_ == 0;
    return tf($_[0], "false");
}
while(defined(my $arg = shift @args)) {
    if ($arg =~ /^-([akK])$/) {
        $RENDER_MODE = $1;
    }
    else {
        $RENDER_CHAR = $arg;
    }
    last;
}

sub Get {
    my $file = $_[0];
    # file name is ord of char being printed, possibly a character style
    # and .svg extention.
    open(F, $file) || die "Failed to open $file for read:$!\\n";
    my (@paths, $charOrd);
    $charOrd = sprintf("%06x",
        hex(($file =~ m{/([\da-zA-Z]{5})})[0]));
    my $renderChar = chr(hex($charOrd));
    my ($width, $height, $h_scale_factor, $v_scale_factor);
    while (<F>) {
        # use width and hight to calculate a scale factor for
        # normalizing char to be 100 x 100 pix
        if (/<svg\s.*width="([^"]+)".*height="([^"]++)/) {
            push @paths, sprintf("C:%s\nWH:%d,%d", $renderChar, $1, $2);
        }
        elsif (/<path/) {
            my $p = ($_ =~ /\s+d="([^"]+)/)[0];
            my @p = grep(length, split(/([a-zA-Z])/, $p));
            my ($op, $vals);
            my @pp;
            while (@p && (($op, $vals) = (shift @p, shift @p))) {
                my @vals = map{sprintf("%.5f", $_)}
                    ($vals =~ m{(-*\d+(?:\.\d+)*)}g);
                # apply scale factor to result in 100x100 pix char.
                push @pp, "$op"
                    . join(',', map {sprintf("%.3f", $_)}@vals);
            }
            push @paths, sprintf("%s", join('', @pp));
        }
    }
    return [@paths];
}
my $HEADER = "# Created by $funcName version $VERSION
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
sub printPathsFile {
    my ($outFileName, $printHeader, @lines) = @_;
    open(F, ">", "$funcDir/$outFileName")
        || die "open $funcDir/$outFileName for write FAILED$!\n";
    binmode(F, ':utf8');
    print F $HEADER if true($printHeader);
    map {print F $_, "\n"} @lines;
    close F;
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
my $outAllFile = ($RENDER_MODE)
    ? ($RENDER_MODE eq 'a')
        ? "kanjivg.all.pat"
        : "kanjivg.kana.pat"
    : undef;
# map char => svg filename.
my $hiragana = qr/[\x{3041}-\x{3096}]/;
my $katakana = qr/[\x{30A0}-\x{30FF}]/;
my $kanji = qr/[\x{3400}-\x{4DB5}\x{4E00}-\x{9FCB}\x{F900}-\x{FA6A}]/;
my %allChars = map {
    my ($ch, $ext) = m{^.*/([\da-f]{5})(-.*)*\.svg$};
    chr(hex($ch)) . ($ext || '') => $_;
} grep m{([\da-f]{5})(?:-[^.]+)*\.svg$}, <$svgDir/*>;

# delete everything not kanji or kana.
foreach my $char (keys %allChars) {
    unless($char =~ $hiragana ||
        $char =~ $katakana ||
        $char =~ $kanji) {
            delete $allChars{$char};
    }
}
my $outCharFile = "$pathsDir/%s.avg";
my (@collectionLines);
my @renderNames = ($RENDER_CHAR)
    ? ($RENDER_CHAR)
    : ($RENDER_MODE eq 'k')
        ? grep($_ =~ $hiragana || $_ =~ $katakana, keys %allChars)
        : ($RENDER_MODE eq 'k')
            ? grep($_ =~ $kanji, keys %allChars)
            : keys %allChars;
foreach my $renderName (sort @renderNames) {
    my $svgFileIn = "$allChars{$renderName}";
    if (-f $svgFileIn) {
        my $renderChar = ($renderName =~ m{^([^-])+})[0];
        my ($paths) = Get($svgFileIn);
        my $kvgPat = join("\n", @$paths, '');
        push @collectionLines, $kvgPat;
        printPathsFile(sprintf($outCharFile, $renderName),
            false(), @$paths);
        printf("$renderChar:%05x ", ord($renderChar));
    }
    else {
        push @missing, "$renderName:$svgFileIn" unless -f $svgFileIn;
    }
}
printPathsFile($outAllFile, true(), @collectionLines) if defined $outAllFile;
print "\n";
die "@missing\n" if @missing;
exit;
