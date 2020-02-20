#ifndef LISP_STRBUF_H
#define LISP_STRBUF_H

#include "types.h"

struct strbuf
{
	uint size, cap;
	char *buf;
};

void strbuf_init(struct strbuf *);
void strbuf_append(struct strbuf *, const char *);
void strbuf_delete(struct strbuf *);

#endif

