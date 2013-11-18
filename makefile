CC=cc
CFLAGS=-Wall -ggdb -O0
MACROS=

lisp:
	$(CC) $(CFLAGS) $(MACROS) lisp.c -o lisp

