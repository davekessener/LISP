#include "oo.h"

void *allocate(size_t n)
{
	void *p = malloc(n);

	memset(p, 0, n);

	return p;
}

char upcase(char c) { return ('a' <= c && c <= 'z') ? (c + 'A' - 'a') : c; }
char downcase(char c) { return ('A' <= c && c <= 'Z') ? (c + 'a' - 'A') : c; }

int strcmpi(const char *a, const char *b)
{
	while(*a || *b)
	{
		if(!*a) return -1;
		if(!*b) return 1;

		if(downcase(*a) != downcase(*b))
			return *a - *b;

		++a, ++b;
	}

	return 0;
}

struct error_log_entry *error_log(void)
{
	static struct error_log_entry e = { 0 };

	return &e;
}

void gdb_assert_hook(void)
{
}

