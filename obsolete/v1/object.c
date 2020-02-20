#include "object.h"

#include "env.h"

#include "strbuf.h"

struct object *obj_true(void)
{
	static struct object t = { 0 };

	if(t.type != OBJT_BOOL)
	{
		t.type = OBJT_BOOL;
		t.content.boolean = TRUE;
	}

	return &t;
}

struct object *obj_false(void)
{
	static struct object t = { 0 };

	if(t.type != OBJT_BOOL)
	{
		t.type = OBJT_BOOL;
		t.content.boolean = FALSE;
	}

	return &t;
}

STATUS object_new(struct object **pself, struct object *that)
BEGIN
	*pself = that;
	++(*pself)->count;
END

STATUS object_new_symbol(struct object **pself, const char *s)
BEGIN
	NEW(object, self);

	self->type = OBJT_SYMBOL;
	self->count = 1;
	self->content.sym = strdup(s);

	*pself = self;
END

STATUS object_new_string(struct object **pself, const char *s)
BEGIN
	RUN(object_new_symbol(pself, s));

	(*pself)->type = OBJT_STRING;
END

STATUS object_new_cell(struct object **pself, struct object *car, struct object *cdr)
BEGIN
	NEW(object, self);

	self->type = OBJT_CELL;
	self->count = 1;
	self->content.cell.car = car;
	self->content.cell.cdr = cdr;

	if(car) ++car->count;
	if(cdr) ++cdr->count;

	*pself = self;
END

STATUS object_new_number(struct object **pself, double v)
BEGIN
	NEW(object, self);

	self->type = OBJT_NUMBER;
	self->count = 1;
	self->content.value = v;

	*pself = self;
END

STATUS object_new_lambda(struct object **pself, struct object *args, struct object *body, struct env *e)
BEGIN
	ASSERT(args);
	ASSERT(body);

	struct object *a = args;

	while(a)
	{
		ASSERT(a->type == OBJT_CELL);
		ASSERT(a->content.cell.car);
		ASSERT(a->content.cell.car->type == OBJT_SYMBOL);

		a = a->content.cell.cdr;
	}

	NEW(object, self);

	self->type = OBJT_LAMBDA;
	self->count = 1;
	self->content.lambda.args = args;
	self->content.lambda.body = body;
	self->content.lambda.environment = e;

	++e->count;
	++args->count;
	++body->count;

	*pself = self;
END

STATUS object_new_builtin(struct object **pself, const char *name, builtin_fn f)
BEGIN
	NEW(object, self);

	self->type = OBJT_BUILTIN;
	self->count = 1;
	self->content.builtin.name = strdup(name);
	self->content.builtin.callback = f;

	*pself = self;
END

const char *num_to_s(double v)
{
	static char BUF[0x100];

	snprintf(BUF, sizeof(BUF)-1, "%.3f%c", v, '\0');

	return BUF;
}

void obj_to_s_impl(struct strbuf *sb, struct object *self, BOOL first)
{
	if(!self)
	{
		strbuf_append(sb, first ? "NIL" : ")");
	}
	else
	{
		if(self->type != OBJT_CELL && !first)
		{
			strbuf_append(sb, " . ");
		}

		switch(self->type)
		{
			case OBJT_SYMBOL:
				strbuf_append(sb, self->content.sym);
				break;

			case OBJT_STRING:
				strbuf_append(sb, "\"");
				strbuf_append(sb, self->content.sym);
				strbuf_append(sb, "\"");
				break;

			case OBJT_CELL:
				strbuf_append(sb, first ? "(" : " ");
				obj_to_s_impl(sb, self->content.cell.car, TRUE);
				obj_to_s_impl(sb, self->content.cell.cdr, FALSE);
				break;

			case OBJT_NUMBER:
				strbuf_append(sb, num_to_s(self->content.value));
				break;

			case OBJT_LAMBDA:
				strbuf_append(sb, "LAMBDA");
				break;

			case OBJT_MACRO:
				strbuf_append(sb, "MACRO");
				break;

			case OBJT_BOOL:
				strbuf_append(sb, self->content.boolean ? "#t" : "#f");
				break;

			case OBJT_BUILTIN:
				strbuf_append(sb, "BUILTIN[");
				strbuf_append(sb, self->content.builtin.name);
				strbuf_append(sb, "]");
				break;

			default:
				strbuf_append(sb, "!INVALID_TYPE!");
				break;
		}

		if(self->type != OBJT_CELL && !first)
		{
			strbuf_append(sb, ")");
		}
	}
}

const char *object_to_s(struct object *self)
{
	static char BUF[0x10000];
	struct strbuf sb;

	strbuf_init(&sb);
	obj_to_s_impl(&sb, self, TRUE);

	uint n = sizeof(BUF)-1;

	if(sb.size < n) n = sb.size;

	memcpy(BUF, (const char *) sb.buf, n);
	BUF[n] = '\0';

	strbuf_delete(&sb);

	return BUF;
}

void object_delete(struct object *self)
{
	if(!self) return;

	if(self->type == OBJT_BOOL)
	{
		self->count = 1;
		return;
	}

	if(!--self->count)
	{
		switch(self->type)
		{
			case OBJT_SYMBOL:
			case OBJT_STRING:
				free(self->content.sym);
				break;

			case OBJT_CELL:
				object_delete(self->content.cell.car);
				object_delete(self->content.cell.cdr);
				break;

			case OBJT_NUMBER:
				break;

			case OBJT_LAMBDA:
				object_delete(self->content.lambda.args);
				object_delete(self->content.lambda.body);
				env_delete(self->content.lambda.environment);
				break;

			case OBJT_BUILTIN:
				ERROR_X("Cannot delete builtin!");
				break;

			default:
				ERROR_X("Unknown obj type %d!", self->type);
				break;
		}

		memset(self, 0, sizeof(*self));

		free(self);
	}
}

