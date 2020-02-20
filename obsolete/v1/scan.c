#include "scan.h"

STATUS scan_number(struct object **, const char **);
STATUS scan_list(struct object **, const char **);
STATUS scan_string(struct object **, const char **);
STATUS scan_quote(struct object **, const char **);
STATUS scan_symbol(struct object **, const char **);
STATUS scan_expr(struct object **, const char **);

BOOL is_alpha(char c) { return 'a' <= downcase(c) && downcase(c) <= 'z'; }
BOOL is_in_s(char c, const char *s) { while(*s) if(*(s++) == c) return TRUE; return FALSE; }
BOOL is_id(char c) { return is_alpha(c) || is_in_s(c, "-_*?><=&|%$/+#!~"); }
BOOL is_popen(char c) { return c == '(' || c == '[' || c == '{'; }
BOOL is_pclose(char o, char c) { return (o == '(' && c == ')') || (o == '[' && c == ']') || (o == '{' && c == '}'); }

STATUS scan_list_impl(struct object **r, const char **s, char popen)
BEGIN
	NEW(object, head);

	RUN(scan_expr(&head, s));

	if(**s == ' ')
	{
		while(**s == ' ') ++*s;

		if(is_pclose(popen, **s))
		{
			goto close_list;
		}
		else
		{
			NEW(object, tail);

			RUN(scan_list_impl(&tail, s, popen));
			RUN(object_new_cell(r, head, tail));
		}
	}
	else if(is_pclose(popen, **s))
	{
close_list:
		++*s;

		RUN(object_new_cell(r, head, NULL));
	}
	else
	{
		ERROR("Unexpected char %c @%s", **s, *s);
	}
END

STATUS scan_list(struct object **r, const char **s)
BEGIN
	char c = **s;

	ASSERT(is_popen(c));

	++*s;

	RUN(scan_list_impl(r, s, c));
END

STATUS scan_string(struct object **r, const char **s)
BEGIN
	ASSERT(**s == '"');

	const char *i1 = ++*s;

	while(**s != '"')
	{
		ASSERT(**s);

		if(**s == '\\')
		{
			++*s;
			ASSERT(**s);
		}

		++*s;
	}

	const char *i2 = *s;
	uint l = i2 - i1;

	char *ss = malloc(l + 1);

	for(uint i = 0 ; i < l ; ++i)
	{
		ss[i] = i1[i];
	}
	ss[l] = '\0';

	++*s;

	RUN(object_new_string(r, ss));

	free(ss);
END

STATUS scan_quote(struct object **r, const char **s)
BEGIN
	ASSERT(**s == '\'');

	++*s;

	ASSERT(**s);

	NEW(object, quote);
	NEW(object, e);

	RUN(object_new_symbol(&quote, "quote"));
	RUN(scan_expr(&e, s));
	RUN(object_new_cell(&e, e, NULL));
	RUN(object_new_cell(r, quote, e));
END

STATUS scan_symbol(struct object **r, const char **s)
BEGIN
	const char *i1 = *s;

	ASSERT(is_id(*i1));

	while(is_id(**s)) ++*s;

	const char *i2 = *s;

	uint l = i2 - i1;

	char *ss = malloc(l + 1);

	for(uint i = 0 ; i < l ; ++i)
	{
		ss[i] = i1[i];
	}
	ss[l] = '\0';

	RUN(object_new_symbol(r, ss));

	free(ss);
END

STATUS scan_number(struct object **r, const char **s)
BEGIN
	double v = 0;
	double f = 0;
	double e = 1;

	if(**s == '-')
	{
		e = -1;
		++*s;
	}

	ASSERT('0' <= **s && **s <= '9');

	while(TRUE)
	{
		char c = **s;

		if(c >= '0' && c <= '9')
		{
			if(f == 0)
			{
				v = v * 10 + (c - '0');
			}
			else
			{
				f *= 0.1;
				v += f * (c - '0');
			}
		}
		else if(c == '.')
		{
			if(f == 0)
			{
				f = 1;
			}
			else
			{
				ERROR("malformed number end %s", *s);
			}
		}
		else
		{
			break;
		}

		++*s;
	}

	object_new_number(r, e * v);
END

STATUS scan_expr(struct object **r, const char **ss)
BEGIN
	const char *s = *ss;

	if(*s == '-' || ('0' <= *s && *s <= '9'))
	{
		RUN(scan_number(r, ss));
	}
	else switch(*s)
	{
		case '(':
		case '[':
		case '{':
			RUN(scan_list(r, ss));
			break;

		case '"':
			RUN(scan_string(r, ss));
			break;

		case '\'':
			RUN(scan_quote(r, ss));
			break;

		case '#':
			if(s[1] == 'f' || s[1] == 'F')
			{
				*r = obj_false();
				*ss += 2;
			}
			else if(s[1] == 't' || s[1] == 'T')
			{
				*r = obj_true();
				*ss += 2;
			}
			else
			{
				ERROR("Invalid bool #%c!", s[1]);
			}
			break;

		case ' ':
			ERROR("Space @%s", s);
			break;

		default:
			RUN(scan_symbol(r, ss));
			break;
	}
END

STATUS scan(struct object **r, const char *s)
BEGIN
	RUN(scan_expr(r, &s));
	ASSERT(!*s);
END

