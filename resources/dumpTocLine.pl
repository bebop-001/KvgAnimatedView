#!/usr/bin/perl

# turn hex ofset/length values into decimal and print to
# stdout dor debugging.

use utf8;
use Encode;
binmode(STDOUT, ':utf8');

my @args = map { decode 'UTF8', $_ } @ARGV;
unless (@args > 0) {
    die "what kanji are you looking for\n";
}
my $tocFile = "paths/avg.toc.txt";
unless( -f $tocFile) { die "couldn't find toc:\"$tocFile\"\n"}
open F, '<', "paths/avg.toc.txt";
binmode(F, ':utf8');
my @toc = <F>;
close F;
my %uniq;
my $i = 1;
for my $arg (@args) {
    for my $line (grep $_ =~ /$arg/, @toc) {
        my $key = ($line =~ m{^(\S+).*})[0];
        if ($uniq{$key}++ != 0) {next}
        printf("%2d) %s", $i++, $key);
        my @parts = split /\s+/, $line;
        for (@parts) {
            if($_ =~ m{(\S+)([0-9a-fA-F]{3})([0-9a-fA-F]{2})$}) {
                my ($a, $b) = map{hex($_)} ($2, $3);
                print "$_:$a:$b, ";
            }
        }
        print "\n";
    }
}
