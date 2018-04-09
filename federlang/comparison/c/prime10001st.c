
#ifdef __cplusplus
extern "C" {
#endif 

#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include <math.h>
#include <stdint.h> // uint32_t

/**
 * @param primes An array of accumalated prime numbers
 * @param len_primes The number of prime numbers stored in 'primes'
 * @param num The number, which should be checked
 * @return Returns true, if num is a prime numbers
 */
bool isprime (uint32_t * primes, int len_primes, uint32_t num);

int main (int lenargs, char ** vargs) {

	int reserved_length = 100;
	int current_length = 3;

	uint32_t * primes = NULL;
	primes = (uint32_t*) malloc (reserved_length * sizeof (uint32_t));
	if (!primes) {
		printf ("Memory error");
		return EXIT_FAILURE;
	}

	// Add three prime numbers to the array
	primes [0] = 2;
	primes [1] = 3;
	primes [2] = 5;

	uint32_t current_number = 6;
	while (current_length < 10001) {

		if (isprime (primes, current_length, current_number)) {
			if (reserved_length == current_length) {
				reserved_length += 100;
				primes = (uint32_t*) realloc (primes, reserved_length * sizeof (uint32_t));
				if (!primes) {
					printf ("Memory error");
					return EXIT_FAILURE;
				}
			}

			// Assign the found prime number
			primes [current_length++] = current_number;
		}
		
		current_number++;
	}

	free (primes);
	printf ("10001st prime: %u\n", (current_number-1));
	
	return EXIT_SUCCESS;
}

bool isprime (uint32_t * primes, int len_primes, uint32_t num) {
	uint32_t max_check = sqrt (num);
	for (int i = 0; i < len_primes; i++) {
		if (primes [i] > max_check) {
			break;
		}

		if (num % primes [i] == 0) {
			return false;
		}
	}

	return true;
}

#ifdef __cplusplus
}
#endif
