#include <stdlib.h>
#include <stdio.h>

int main (int lenargs, char ** vargs) {
	int n0 = 5;
	int n1 = 7;
	int result = 0;

	int i = 0;
	while (i < 100) {
		result = n0 * n1;
		i++;
	}

	printf ("%d\n", result);

	return EXIT_SUCCESS;
}
