#include "strbuf.h"

void strbuf_init(struct strbuf *self)
{
	self->size = self->cap = 0;
	self->buf = NULL;
}

void strbuf_append(struct strbuf *self, const char *s)
{
	uint n = strlen(s);

	while(self->size + n > self->cap)
	{
		self->buf = realloc(self->buf, self->cap = self->cap * 2 + 1);
	}

	while(*s)
	{
		self->buf[self->size++] = *s++;
	}

	self->buf[self->size] = '\0';
}

void strbuf_delete(struct strbuf *self)
{
	free(self->buf);
	memset(self, 0, sizeof(*self));
}

