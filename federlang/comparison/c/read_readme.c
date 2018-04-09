#include <stdlib.h>
#include <stdio.h>

int main (int lenargs, char ** vargs) {
	FILE * pfile = fopen ("jfederc", "r");
	if (!pfile) {
		return EXIT_FAILURE;
	}

	char c;
	while (!feof (pfile) && (c = fgetc (pfile))) {
		putchar (c);
	}

	fclose (pfile);

	return EXIT_SUCCESS;
}
