#ifndef LISP_OO_H
#define LISP_OO_H

#include "types.h"
#include "varbuf.h"

typedef int STATUS;

enum
{
	STATUS_OK = 0,
	STATUS_ERR = 1
};

#define NEW(t,n) struct t *n = allocate(sizeof(struct t))

#define RETURN(v) do { varbuf_delete(&var_buf_); return v; } while(0)

#define DONE RETURN(STATUS_OK)

#define BEGIN { struct varbuf var_buf_; varbuf_init(&var_buf_);
#define END DONE; }

#define LOCAL(t,n) struct t *n = NULL; varbuf_push(&var_buf_, &n, (varbuf_cb) & t##_delete )

#define RUN(s) do { if(s) RETURN(STATUS_ERR); } while(0)

#define REPORT(e) \
	error_log()->file = __FILE__; \
	error_log()->line = __LINE__; \
	error_log()->error = e;

#define ERROR(s,...) do { \
	ERROR_X(s,__VA_ARGS__); \
	REPORT(s); \
	RETURN(STATUS_ERR); \
} while(0)

#define ERROR_X(...) do { printf(__VA_ARGS__); printf("\n"); } while(0)

void *allocate(size_t);
char upcase(char);
char downcase(char);
int strcmpi(const char *, const char *);

struct error_log_entry
{
	const char *file;
	const char *error;
	int line;
};

struct error_log_entry *error_log( );

#define ASSERT(e,...) do { \
	if(!(e)) { \
		REPORT( #e ); \
		gdb_assert_hook(); \
		RETURN(STATUS_ERR); \
	} \
} while(0)

void gdb_assert_hook( );

#endif

