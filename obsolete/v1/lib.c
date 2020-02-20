#include "lib.h"

#include "lisp.h"

void env_add(struct env **e, const char *s, builtin_fn f)
{
	struct object *o;

	object_new_builtin(&o, s, f);

	env_new(e, s, o, *e);
}

struct env *default_env(void)
{
	static struct env *e = NULL;

	if(!e)
	{
		env_add(&e, "BEGIN", &builtin_begin);
		env_add(&e, "LAMBDA", &builtin_lambda);
		env_add(&e, "+", &builtin_add);
		env_add(&e, "-", &builtin_sub);
		env_add(&e, "*", &builtin_mul);
		env_add(&e, "/", &builtin_div);
	}

	return e;
}

struct object *pop(struct object *x)
{
	struct object *r = x->content.cell.cdr;

	if(r)
	{
		++r->count;
	}

	object_delete(x);

	return r;
}

STATUS helper_check_correct_builtin(struct object *x, const char *id)
BEGIN
	ASSERT(x);
	ASSERT(x->type == OBJT_CELL);
	ASSERT(x->content.cell.car);
	ASSERT(x->content.cell.car->type == OBJT_SYMBOL);
	ASSERT(!strcmpi(x->content.cell.car->content.sym, id));
END

double num_add(double a, double b) { return a + b; }
double num_sub(double a, double b) { return a - b; }
double num_mul(double a, double b) { return a * b; }
double num_div(double a, double b) { return a / b; }

STATUS helper_num_op(struct result *r, struct object *x, struct env *e, const char *op, double (*f)(double, double))
BEGIN
	RUN(helper_check_correct_builtin(x, op));

	r->value = NULL;
	r->environment = e;

	LOCAL(object, args);

	RUN(eval_all(&args, x->content.cell.cdr, e));

	ASSERT(args);
	ASSERT(args->type == OBJT_CELL);

	struct object *ov = args->content.cell.car;
	struct object *xi = args->content.cell.cdr;

	ASSERT(ov);
	ASSERT(ov->type == OBJT_NUMBER);

	double v = ov->content.value;

	while(xi)
	{
		ASSERT(xi->type == OBJT_CELL);

		ov = xi->content.cell.car;
		xi = xi->content.cell.cdr;

		ASSERT(ov);
		ASSERT(ov->type == OBJT_NUMBER);

		v = (*f)(v, ov->content.value);
	}

	RUN(object_new_number(&r->value, v));
END

STATUS builtin_begin(struct result *r, struct object *x, struct env *e)
BEGIN
	RUN(helper_check_correct_builtin(x, "begin"));

	r->value = NULL;
	r->environment = e;

	while(x)
	{
		ASSERT(x->type == OBJT_CELL);
		ASSERT(x->content.cell.car);

		eval(r, x->content.cell.car, r->environment);

		x = pop(x);
	}
END

STATUS builtin_lambda(struct result *r, struct object *x, struct env *e)
BEGIN
	RUN(helper_check_correct_builtin(x, "lambda"));

	r->value = NULL;
	r->environment = e;

	struct object *x1 = x->content.cell.cdr;

	ASSERT(x1);
	ASSERT(x1->type == OBJT_CELL);

	struct object *x2 = x1->content.cell.cdr;

	ASSERT(x2);
	ASSERT(x2->type == OBJT_CELL);
	ASSERT(!x2->content.cell.cdr);

	struct object *args = x1->content.cell.car;
	struct object *body = x2->content.cell.car;

	ASSERT(args);
	ASSERT(args->type == OBJT_CELL);
	ASSERT(body);
	ASSERT(body->type == OBJT_CELL);

	RUN(object_new_lambda(&r->value, args, body, e));
END

STATUS builtin_add(struct result *r, struct object *x, struct env *e)
BEGIN
	RUN(helper_num_op(r, x, e, "+", &num_add));
END

STATUS builtin_sub(struct result *r, struct object *x, struct env *e)
BEGIN
	RUN(helper_num_op(r, x, e, "-", &num_sub));
END

STATUS builtin_mul(struct result *r, struct object *x, struct env *e)
BEGIN
	RUN(helper_num_op(r, x, e, "*", &num_mul));
END

STATUS builtin_div(struct result *r, struct object *x, struct env *e)
BEGIN
	RUN(helper_num_op(r, x, e, "/", &num_div));
END

