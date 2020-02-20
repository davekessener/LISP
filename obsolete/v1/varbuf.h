#ifndef LISP_VARBUF_H
#define LISP_VARBUF_H

#include "types.h"

typedef void (*varbuf_cb)(void *);

struct varbuf
{
	uint size, cap;
	struct
	{
		void *var;
		varbuf_cb cb;
	} *content;
};

void varbuf_init(struct varbuf *);
void varbuf_push(struct varbuf *, void *, varbuf_cb);
void varbuf_top(struct varbuf *, void *);
void varbuf_pop(struct varbuf *);
void varbuf_delete(struct varbuf *);

#endif

