#include <libintl.h>

/*
 * Checks for the existence of libintl on the system. First attempt to
 * link *without* -lintl since some versions of glibc come with these
 * functions built-in. If this fails, then try linking *with* -lintl.
 */
int main()
{
	bindtextdomain(0, 0);
	return 0;
}
