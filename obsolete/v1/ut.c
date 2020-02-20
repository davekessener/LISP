#include "ut.h"

#include "oo.h"
#include "strbuf.h"

#define UT_BEGIN(id) \
void test_##id (void) { \
	static const char *module_name_ = #id ;

#define UT_END }

#define UT_ASSERT(c) \
do { \
	++*ut_tests(); \
	if(!(c)) { \
		ut_push_error(module_name_, #c, __LINE__); \
		++*ut_errors(); \
		printf("F"); \
	} else { \
		printf("."); \
	} \
} while(0)

struct entry
{
	char *module;
	char *error;
	int line;
	struct entry *next;
};

int *ut_errors(void)
{
	static int v = 0;

	return &v;
}

int *ut_tests(void)
{
	static int v = 0;

	return &v;
}

struct entry **ut_error_log(void)
{
	static struct entry *e = NULL;

	return &e;
}

void ut_push_error(const char *module, const char *error, int line)
{
	struct entry **log = ut_error_log();
	NEW(entry, e);

	e->module = strdup(module);
	e->error = strdup(error);
	e->line = line;
	e->next = *log;

	*log = e;
}

void ut_print_error(struct entry *log)
{
	if(!log) return;

	ut_print_error(log->next);

	printf("Error in %s:%d [%s]\n", log->module, log->line, log->error);
}

UT_BEGIN(strbuf)
	struct strbuf sb;

	strbuf_init(&sb);

	UT_ASSERT(sb.size == 0);
	UT_ASSERT(sb.cap == 0);
	UT_ASSERT(!sb.buf);

	strbuf_append(&sb, "Hello, World!");

	UT_ASSERT(sb.size == 13);
	UT_ASSERT(sb.cap >= sb.size);
	UT_ASSERT(sb.buf);
	UT_ASSERT(!strcmp(sb.buf, "Hello, World!"));

	strbuf_append(&sb, "abcABC");

	UT_ASSERT(sb.size == 19);
	UT_ASSERT(sb.cap >= sb.size);
	UT_ASSERT(sb.buf);
	UT_ASSERT(!strcmp(sb.buf, "Hello, World!abcABC"));
UT_END

UT_BEGIN(strop)
	UT_ASSERT(!strcmpi("abcde", "abcde"));
	UT_ASSERT(!strcmpi("abcde", "aBcDe"));
	UT_ASSERT(!strcmpi("AbCdE", "ABCDE"));
	UT_ASSERT(!strcmpi("abcde", "ABCDE"));
	UT_ASSERT( strcmpi("abcde", "uvwxy"));
	UT_ASSERT( strcmpi("abcde", "abcdx"));
	UT_ASSERT( strcmpi("abcde", "xbcde"));
	UT_ASSERT( strcmpi("abcde", "abcd"));
	UT_ASSERT( strcmpi("abcde", "bcde"));
UT_END

BOOL run_ut(void)
{
	test_strbuf();
	test_strop();

	printf("\n%d OK, %d errors.\n", *ut_tests(), *ut_errors());

	ut_print_error(*ut_error_log());

	return !*ut_errors();
}

