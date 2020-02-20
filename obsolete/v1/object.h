#ifndef LISP_OBJECT_H
#define LISP_OBJECT_H

#include "oo.h"

enum
{
	OBJT_SYMBOL,
	OBJT_STRING,
	OBJT_CELL,
	OBJT_NUMBER,
	OBJT_BOOL,
	OBJT_LAMBDA,
	OBJT_MACRO,
	OBJT_BUILTIN
};

typedef int obj_type;

struct object;
struct env;
struct result;

typedef STATUS (*builtin_fn)(struct result *, struct object *, struct env *);

struct object
{
	obj_type type;
	uint count;
	union
	{
		char *sym;
		struct
		{
			struct object *car;
			struct object *cdr;
		} cell;
		double value;
		struct
		{
			struct object *args;
			struct object *body;
			struct env *environment;
		} lambda;
		struct
		{
			char *name;
			builtin_fn callback;
		} builtin;
		BOOL boolean;
	} content;
};

struct result
{
	struct object *value;
	struct env *environment;
};

struct object *obj_true( );
struct object *obj_false( );

STATUS object_new(struct object **, struct object *);
STATUS object_new_symbol(struct object **, const char *);
STATUS object_new_string(struct object **, const char *);
STATUS object_new_cell(struct object **, struct object *, struct object *);
STATUS object_new_number(struct object **, double);
STATUS object_new_lambda(struct object **, struct object *, struct object *, struct env *);
STATUS object_new_builtin(struct object **, const char *, builtin_fn);
const char *object_to_s(struct object *);
void object_delete(struct object *);

#endif

