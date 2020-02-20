#include "lisp.h"

STATUS build_closure(struct env **r, struct object *a, struct object *p, struct env *e)
BEGIN
	if(!a && !p)
	{
		*r = e;
		DONE;
	}

	ASSERT(a);
	ASSERT(p);
	ASSERT(p->type == OBJT_CELL);

	LOCAL(env, t);

	RUN(build_closure(&t, a->content.cell.cdr, p->content.cell.cdr, e));

	RUN(env_new(r, a->content.cell.car->content.sym, p->content.cell.car, t));

	t = NULL;
END

STATUS apply(struct result *r, struct object *f, struct object *p, struct env *e)
BEGIN
	ASSERT(f);
	ASSERT(p);
	ASSERT(f->type == OBJT_LAMBDA);
	ASSERT(p->type == OBJT_CELL);

	LOCAL(env, closure);

	RUN(build_closure(&closure, f->content.lambda.args, p, e));

	RUN(eval(r, f->content.lambda.body, closure));
END

STATUS eval_all(struct object **r, struct object *a, struct env *e)
BEGIN
	if(!a) DONE;

	struct result tmp = { 0 };
	LOCAL(object, head);
	LOCAL(object, tail);
	
	RUN(eval(&tmp, a->content.cell.car, e));
	head = tmp.value;
	RUN(eval_all(&tail, a->content.cell.cdr, e));
	RUN(object_new_cell(r, head, tail));

	head = tail = NULL;
END

STATUS eval(struct result *r, struct object *x, struct env *e)
BEGIN
	ASSERT(x);

	struct result f = { 0 };
	struct object *a = NULL;

	r->environment = e;

	switch(x->type)
	{
		case OBJT_CELL:
			ASSERT(x->content.cell.car);
			RUN(eval(&f, x->content.cell.car, e));
			ASSERT(f.value);
			switch(f.value->type)
			{
				case OBJT_LAMBDA:
					RUN(eval_all(&a, x->content.cell.cdr, e));
					RUN(apply(r, f.value, a, e));
					break;

				case OBJT_BUILTIN:
					RUN(f.value->content.builtin.callback(r, x, e));
					break;

				case OBJT_MACRO:
					break;

				default:
					ERROR("Not executeable: %s", object_to_s(x));
			}
			break;

		case OBJT_SYMBOL:
			RUN(env_lookup(e, &r->value, x->content.sym));
			break;

		default:
			r->value = x;
			break;
	}
END

