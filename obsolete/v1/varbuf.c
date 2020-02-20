#include "varbuf.h"

void varbuf_init(struct varbuf *self)
{
	self->size = self->cap = 0;
	self->content = NULL;
}

void varbuf_push(struct varbuf *self, void *c, varbuf_cb f)
{
	if(self->size == self->cap)
	{
		self->content = realloc(self->content, sizeof(*self->content) * (self->cap = self->cap * 2 + 1));
	}

	self->content[self->size].var = c;
	self->content[self->size].cb  = f;

	++self->size;
}

void varbuf_top(struct varbuf *self, void *r)
{
	*((void **) r) = self->content[self->size].var;
}

void varbuf_pop(struct varbuf *self)
{
	--self->size;

	if(self->size < self->cap / 4)
	{
		self->content = realloc(self->content, self->cap /= 2);
	}
}

void varbuf_delete(struct varbuf *self)
{
	for(uint i = self->size ; i ; --i)
	{
		void *e = *((void **) self->content[i-1].var);

		if(e)
		{
			self->content[i-1].cb(e);
		}
	}
}

