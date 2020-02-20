#ifndef LISP_LISP_H
#define LISP_LISP_H

#include "oo.h"
#include "object.h"
#include "env.h"

STATUS eval(struct result *, struct object *, struct env *);
STATUS eval_all(struct object **, struct object *, struct env *);

#endif

