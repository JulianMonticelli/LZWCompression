This program can both compress and decompress files using LZW compression. 

Several of the files required to run this program are property of Robert Sedgewick and Kevin Wayne, from Algorithms, 4th edition. For this reason, I could not include them.

If you are interested in running, the files are:

```BinaryStdIn.java, BinaryStdOut.java, Queue.java, StdIn.java, StdOut.java, TST.java```

and you must acquire them through your own means.

MyLZW uses IO redirection, and depending on your operating system the commands to run the program will be different.

In __Windows__, compressing a file can be done as:

```java MyLZW - -MODE < infile > compressedFile```

and decompressing can be done by:

```java MyLZW + < compressedFile > decompressedFile```

In Linux/Mac the equivalent commands should be:

```cat infile > java MyLZW - -MODE > compressedFile```

and decompressing can be done by:

```cat compressedFile > java MyLZW + > decompressedFile```



Where __MODE__ is either

__n__: normal LZW compression
__r__: reset mode (resets LZW dictionary after it is full)
__m__: monitor mode (resets LZW dictionary after a certain compression threshold has been met)


Notice: There was a bug that slipped past me when I turned this program. If I remember correctly, there is a bug somewhere along the lines either in compression or decompression of monitor mode. I never fixed this, as I had already turned the project in before finding out. However, coming back and looking at this code makes me feel like I have incomplete work. I may return to this at a future date and fix the code such that the program works flawlessly.