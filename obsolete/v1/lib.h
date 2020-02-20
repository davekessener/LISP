#ifndef LISP_LIB_H
#define LISP_LIB_H

#include "oo.h"
#include "object.h"
#include "env.h"

STATUS builtin_begin(struct result *, struct object *, struct env *);
STATUS builtin_lambda(struct result *, struct object *, struct env *);
STATUS builtin_add(struct result *, struct object *, struct env *);
STATUS builtin_sub(struct result *, struct object *, struct env *);
STATUS builtin_mul(struct result *, struct object *, struct env *);
STATUS builtin_div(struct result *, struct object *, struct env *);

struct env *default_env( );

#endif

