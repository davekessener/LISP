#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>

#include "oo.h"
#include "object.h"
#include "env.h"
#include "lisp.h"
#include "lib.h"
#include "scan.h"
#include "ut.h"

void run(void)
{
	struct env *environment = default_env();

	printf("%s\n", env_to_s(environment));

	struct object *o = NULL;

	STATUS v = scan(&o, "((lambda (x) (* x x)) 7)");

	if(v)
	{
		printf("Parser error in %s:%d [%s]\n", error_log()->file, error_log()->line, error_log()->error);
	}
	else
	{
		printf("> %s\n", object_to_s(o));

		struct result r = { 0 };

		v = eval(&r, o, environment);

		if(v)
		{
			printf("Runtime error in %s:%d [%s]\n", error_log()->file, error_log()->line, error_log()->error);
		}
		else
		{
			printf("%s\n", object_to_s(r.value));
		}
	}
}

int main(int argc, char **argv)
{
	if(run_ut())
	{
		run();
	}

	return EXIT_SUCCESS;
}

