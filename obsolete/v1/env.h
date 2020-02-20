#ifndef LISP_ENV_H
#define LISP_ENV_H

#include "oo.h"
#include "object.h"

struct env
{
	char *name;
	struct object *value;
	uint count;
	struct env *rest;
};

STATUS env_new_copy(struct env **, struct env *);
STATUS env_new(struct env **, const char *, struct object *, struct env *);
STATUS env_lookup(struct env *, struct object **, const char *);
const char *env_to_s(struct env *);
void env_delete(struct env *);

#endif

