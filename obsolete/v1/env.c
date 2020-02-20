#include "env.h"

#include "strbuf.h"

STATUS env_new_copy(struct env **pself, struct env *that)
BEGIN
	*pself = that;

	++(*pself)->count;
END

STATUS env_new(struct env **pself, const char *name, struct object *value, struct env *rest)
BEGIN
	NEW(env, self);

	self->name = strdup(name);
	self->value = value;
	self->count = 1;
	self->rest = rest;

	if(value) ++self->value->count;
	if(rest) ++self->rest->count;

	*pself = self;
END

STATUS env_lookup(struct env *self, struct object **r, const char *s)
BEGIN
	ASSERT(self);
	ASSERT(!*r);
	ASSERT(s);

	if(strcmpi(self->name, s) == 0)
	{
		*r = self->value;
	}
	else
	{
		RUN(env_lookup(self->rest, r, s));
	}
END

const char *env_to_s(struct env *self)
{
	static char BUF[0x10000];
	struct strbuf sb;

	strbuf_init(&sb);

	strbuf_append(&sb, "{");

	while(self)
	{
		strbuf_append(&sb, self->name);
		strbuf_append(&sb, " => ");
		strbuf_append(&sb, object_to_s(self->value));

		self = self->rest;

		if(self)
		{
			strbuf_append(&sb, ", ");
		}
	}

	strbuf_append(&sb, "}");

	uint n = sizeof(BUF) - 1;

	if(sb.size < n) n = sb.size;

	memcpy(BUF, (const char *) sb.buf, n);
	BUF[n] = '\0';

	strbuf_delete(&sb);

	return BUF;
}

void env_delete(struct env *self)
{
	if(!self) return;

	if(!--self->count)
	{
		free(self->name);
		object_delete(self->value);
		env_delete(self->rest);
		memset(self, 0, sizeof(*self));
		free(self);
	}
}

