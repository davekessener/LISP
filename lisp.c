#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

#define STRBUF_SIZE 1024

struct __lisp_cell;
struct __cell_list;
struct __string_list;
struct __environment;

enum cell_type
{
	SYMBOL,
	NUMBER,
	LIST,
	PROC,
	LAMBDA
};

typedef long double BIGNUM;
typedef char * C_STR;
typedef const C_STR CC_STR;
typedef C_STR * STRP;
typedef int (*proc_fn)(struct __lisp_cell *, struct __cell_list);

typedef struct __lisp_cell
{
	union
	{
		struct __cell_list *cells;
		struct __environment *env;
		BIGNUM number;
		proc_fn proc;
	} car;
	enum cell_type ctype;
	CC_STR id;
} LCELL;

typedef struct __environment
{
	struct __environment *outer;
	struct
	{
		STRP ids;
		struct __lisp_cell *cells;
		int c;
	} map;
} ENV;

typedef struct __string_list
{
	STRP strings;
	int c;
} STRINGS;

typedef struct __cell_list
{
	struct __lisp_cell *cells;
	int c;
} CELLS;

void stdLogFn(CC_STR msg)
{
	fprintf(stderr, "%s\n", msg);
}

typedef void (*logFn)(CC_STR);
static logFn *logFunction(void)
{
	static logFn lfn = stdLogFn;

	return &lfn;
}
#define DLOG(s) (*logFunction())(s)

int ENV_init(ENV *this, CELLS parameter, CELLS arguments, ENV *outer)
{
	int i;
	char strbuf(STRBUF_SIZE);

	this->map.c = parameter.c;
	this->map.ids = malloc(this->map.c * sizeof(C_STR));
	this->map.cells = malloc(this->map.c * sizeof(LCELL *));

	for(i = 0 ; i < parameter.c ; i++)
	{
		if(parameter.cells[i].ctype != SYMBOL)
		{
			sprintf(strbuf, "ERR: Parameter #%d'%s' is not a symbol, cannot initialize ENV.\n", i, parameter.cells[i].id);
			DLOG(strbuf);
			return 1;
		}

		if(i >= arguments.c)
		{
			sprintf(strbuf, "ERR: Not enough arguments for %d parameter in ENV_init!\n", parameter.c);
			DLOG(strbuf);
			return 2;
		}

		if(parameter.cells[i].id[0] == '*')
		{
			this->map.ids[i] = strdup(parameter.cells[i].id + 1);

			if(arguments.cells[i].ctype == LIST && arguments.c - 1 == i)
			{
				this->map.cells[i] = &arguments.cells[i];
			}
			else
			{
				this->map.cells[i] = malloc(sizeof(LCELL));
				this->map.cells[i]->ctype = LIST;
				this->map.cells[i]->car.
			}
		}
		else
		{
		}
	}
}

// PRIMITIVES
static const LCELL *symTrue(void)
{
	static LCELL t = { (struct __environment *) NULL, SYMBOL, "#t" };

	return &t;
}

static const LCELL *symFalse(void)
{
	static LCELL t = { (struct __environment *) NULL, SYMBOL, "#f" };

	return &t;
}

static const LCELL *symNil(void)
{
	static LCELL t = { (struct __environment *) NULL, SYMBOL, "nil" };

	return &t;
}

int proc_add(LCELL *cell, CELLS params)
{
	int i;
	char strbuf[STRBUF_SIZE];

	if(params.c < 2)
	{
		DLOG("ERR: Too few arguments for primitive 'add'.");
		return 1;
	}

	BIGNUM v = 0;
	
	for(i = 0 ; i < params.c ; i++)
	{
		if(params.cells[i].ctype != NUMBER)
		{
			sprintf(strbuf, "ERR: Argument #%d'%s' has to be a number!", i, params.cells[i].id);
			DLOG(strbuf);
			return 2;
		}
		else
		{
			v += params.cells[i].car.number;
		}
	}

	sprintf(strbuf, "%Lg", (long double) v);
	cell->ctype = NUMBER;
	cell->car.number = v;
	cell->id = strdup(strbuf);

	return 0;
}

int proc_sub(LCELL *cell, CELLS params)
{
	int i;
	char strbuf[STRBUF_SIZE];

	if(params.c < 2)
	{
		DLOG("ERR: Too few arguments for primitive 'sub'.");
		return 1;
	}

	BIGNUM v = 0;
	
	for(i = 0 ; i < params.c ; i++)
	{
		if(params.cells[i].ctype != NUMBER)
		{
			sprintf(strbuf, "ERR: Argument #%d'%s' has to be a number!", i, params.cells[i].id);
			DLOG(strbuf);
			return 2;
		}
		else if(i == 0)
		{
			v = params.cells[i].car.number;
		}
		else
		{
			v -= params.cells[i].car.number;
		}
	}

	sprintf(strbuf, "%Lg", (long double) v);
	cell->ctype = NUMBER;
	cell->car.number = v;
	cell->id = strdup(strbuf);

	return 0;
}

int proc_mul(LCELL *cell, CELLS params)
{
	int i;
	char strbuf[STRBUF_SIZE];

	if(params.c < 2)
	{
		DLOG("ERR: Too few arguments for primitive 'mul'.");
		return 1;
	}

	BIGNUM v = 1;
	
	for(i = 0 ; i < params.c ; i++)
	{
		if(params.cells[i].ctype != NUMBER)
		{
			sprintf(strbuf, "ERR: Argument #%d'%s' has to be a number!", i, params.cells[i].id);
			DLOG(strbuf);
			return 2;
		}
		else
		{
			v *= params.cells[i].car.number;
		}
	}

	sprintf(strbuf, "%Lg", (long double) v);
	cell->ctype = NUMBER;
	cell->car.number = v;
	cell->id = strdup(strbuf);

	return 0;
}

int proc_div(LCELL *cell, CELLS params)
{
	int i;
	char strbuf[STRBUF_SIZE];

	if(params.c < 2)
	{
		DLOG("ERR: Too few arguments for primitive 'div'.");
		return 1;
	}

	BIGNUM v = 0;
	
	for(i = 0 ; i < params.c ; i++)
	{
		if(params.cells[i].ctype != NUMBER)
		{
			sprintf(strbuf, "ERR: Argument #%d'%s' has to be a number!", i, params.cells[i].id);
			DLOG(strbuf);
			return 2;
		}
		else if(i == 0)
		{
			v = params.cells[i].car.number;
		}
		else
		{
			v *= params.cells[i].car.number;
		}
	}

	sprintf(strbuf, "%Lg", (long double) v);
	cell->ctype = NUMBER;
	cell->car.number = v;
	cell->id = strdup(strbuf);

	return 0;
}

int proc_pow(LCELL *cell, CELLS params)
{
	int i;
	char strbuf[STRBUF_SIZE];

	if(params.c < 2)
	{
		DLOG("ERR: Too few arguments for primitive 'pow'.");
		return 1;
	}

	BIGNUM v = 0;
	
	for(i = 0 ; i < params.c ; i++)
	{
		if(params.cells[i].ctype != NUMBER)
		{
			sprintf(strbuf, "ERR: Argument #%d'%s' has to be a number!", i, params.cells[i].id);
			DLOG(strbuf);
			return 2;
		}
		else if(i == 0)
		{
			v = params.cells[i].car.number;
		}
		else
		{
			v = (BIGNUM) pow((double) v, (double) params.cells[i].car.number);
		}
	}

	sprintf(strbuf, "%Lg", (long double) v);
	cell->ctype = NUMBER;
	cell->car.number = v;
	cell->id = strdup(strbuf);

	return 0;
}

int proc_mod(LCELL *cell, CELLS params)
{
	int i;
	char strbuf[STRBUF_SIZE];

	if(params.c != 2)
	{
		sprintf(strbuf, "ERR: Wrong amount of arguments (%d) for primitive 'mod'.", params.c);
		DLOG(strbuf);
		return 1;
	}

	BIGNUM v = 0, s = 1, t;
	
	for(i = 0 ; i < 2 ; i++)
	{
		if(params.cells[i].ctype != NUMBER)
		{
			sprintf(strbuf, "ERR: Argument #%d'%s' has to be a number!", i, params.cells[i].id);
			DLOG(strbuf);
			return 2;
		}
		else if(i == 0)
		{
			v = params.cells[i].car.number;
		}
		else
		{
			t = params.cells[i].car.number; t = t > 0 ? t : -t;
			s = v > 0 ? 1 : -1; v = v > 0 ? v : -v;
			while(v > t) v -= t;
			v *= s;
		}
	}

	sprintf(strbuf, "%Lg", (long double) v);
	cell->ctype = NUMBER;
	cell->car.number = v;
	cell->id = strdup(strbuf);

	return 0;
}



int main (int argc, char *argv[])
{
	return EXIT_SUCCESS;
}

