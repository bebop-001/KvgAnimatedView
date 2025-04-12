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
my $USAGE = "USAGE: $funcName [-f=svgFile] [-k|-K|-a] [char]
    -k  : Create only the kana files
    -K  : Create only the Kanji files (default)
    -a  : Create all files
    char: Create a path file for 'char' only.
    Kvg svg files should be under the svg directory under the
    path to this executable.  Path files are put in the current
    directory.
";
die $USAGE, "\nNo arguments.\n" unless @ARGV;
my $svgFilesDir = "$funcDir/kanji";
unless (-d $svgFilesDir) {
    die $USAGE, "Path output directory $svgFilesDir not found.\n"}
my $pathFileDir = "$funcDir/paths";
unless (-d $pathFileDir) {
    die $USAGE, "Path output directory $pathFileDir not found.\n"}
my @args = @ARGV;
my ($RENDER_CHAR, $RENDER_MODE, $RENDER_FILE) = (undef, "kanji", undef);
while(defined(my $arg = shift @args)) {
    if ($arg =~ /-f=(.*)/) {
        if (-f $1) {
            $RENDER_FILE = $1 }
        else {
            die "$arg: $1 not a file.\n"}
    }
    elsif ($arg =~ /^-([akK])$/) {
        $RENDER_MODE = ($1 eq "a") ? "all"
            : ($1 eq "k") ? "kana"
                : "kanji";
    }
    else {
        $RENDER_CHAR = $arg;
    }
    last;
}
unless (defined $RENDER_MODE || defined $RENDER_FILE) {
    die $USAGE, "Please use render mofe '-a', '-k', or '-K'.\n";
}

# map char => svg filename.
my %chrRange = (
    hiragana => qr/[\x{3041}-\x{3096}]/,
    katakana => qr/[\x{30A0}-\x{30FF}]/,
    kanji => qr/[\x{3400}-\x{4DB5}\x{4E00}-\x{9FCB}\x{F900}-\x{FA6A}]/,
);
my $ordRegex = qr{^.*/([a-fA-F0-9]{5})};
sub toOrd { ($_[0] =~ $ordRegex) [0] }
sub toChar { chr(hex(toOrd($_[0]))) }
sub ordIsIn {
    my ($name, $range) = @_;
    my $chr = toChar($name);
    return ($range eq "all") ? $chr
        : ($chr =~ $chrRange{$range}) ? $chr
            : undef
}
my @renderFiles = (defined $RENDER_FILE)
    ? ( $RENDER_FILE )
    : grep defined ordIsIn($_, $RENDER_MODE), <$svgFilesDir/*.svg>;

my $pathRegex = qr{<path\s+id=.*s(\d+)".*\sd="([^"]+)"};
my $widthRegex = qr {
    <svg\s.*width="([^"]+)".*height="([^"]++)
}x;
# It turns out that for the text transform matrix used by
# kanjiVG, the last two values are the x/y location for text
# placement.
my $textRegex = qr{^\s*<text.*matrix\(([^>]+\D+\d+)};
my @failed;
sub Get {
    my @curXY;
    my @pathInfo;
    my $file = $_[0];
    open(F, $file) || die "Failed to open $file for read:$!\\n";
    my ($width, @paths, @annotations, $fName, $renderedChar);
    my $lineNumber = 0;
    $fName = basename $file;
    $renderedChar = toChar($file);
    while (<F>) {
        chomp;
        $lineNumber++;
        if ($_ =~ $pathRegex) {
            push @paths, "S$1$2" }
        elsif($_=~ $textRegex) { 
            my($x, $y, $id) = ($1 =~ m{([\d.]+)}g)[-3..-1];
            push @annotations, "X$id,$x,$y,$id";
        }
        elsif ($_ =~ $widthRegex) {
            $width = "W$1,$2" }
        else {
            # print "$_\n";
        }
    }
    if ($#paths != $#annotations) {
        my %ids;
        for (@paths, @annotations) {
            my ($type, $id) = m{^(.(\d+))};
            push @{$ids{$id}}, $type;
        }
        my @missing = grep scalar @{$ids{$_}} != 2, keys %ids;
        my @expected;
        for my $i (sort {$a <=> $b} @missing) {
            push @expected, ($ids{$i}[0] =~ /^S/)
                ? "$ids{$i}[0]: no text"
                : "$ids{$i}[0]: no path";
        }
        push (@failed, "$renderedChar:$fName: " .
            scalar @paths . " paths: " .
            join(", ", @expected));
    }
    push @pathInfo, 'N' . $fName, 'C' . $renderedChar, $width;
    # interleave paths and annotation so path annotation
    # is displayed as path is finished.
    foreach my $i (0..$#paths) {
        push @pathInfo, $paths[$i] if(defined $paths[$i]);
        push @pathInfo, $annotations[$i] if(defined $annotations[$i]);
    }
    return @pathInfo;
}
sub getAvgFile {
    my $svgFile = shift @_;
    if (-f $svgFile) {
        my ($ord, $other) = $svgFile =~ m{/([0-9a-fA-f]{5})([^.]+)*.svg$};
        my $renderedChar = chr(hex($ord));
        my @paths = Get($svgFile);
        my $pathFile = "$pathFileDir/$renderedChar" . ($other || '') . ".avg";
        open (OUT2, "> $pathFile") || die "open $pathFile for output FAILED:$!\n";
        binmode(OUT2, ':utf8');
        if (grep !defined $_, @paths) {
            push @failed, "$svgFile: undef.";
            print "* ";
        }
        else {
            print OUT2 join("\n", @paths, "");
            print "$renderedChar ";
        }
        close OUT2;
    }
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
    getAvgFile($renderFile);
}
if (@failed) {
    @failed =  map {$_ =~ s{:[^:]+/}{}; $_ } @failed;
    print STDERR "Problems:\n\t", join("\n\t", @failed), "\n";
}
exit;
