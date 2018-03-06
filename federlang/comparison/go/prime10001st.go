package  main

import (
	"fmt"
	"math"
)

func IsPrime (primes []uint32, num uint32) bool {
	max_num := uint32(math.Sqrt (float64(num)))
	len_primes := len (primes)
	for  i := 0; i <  len_primes; i++ {
		if primes[i] > max_num {
			return true
		}


		if (num % primes[i]) == 0 {
			return false
		}
	}

	return true
}

func main() {
	var primes []uint32

	primes = append(primes, uint32(2))
	primes = append(primes, uint32(3))
	primes = append(primes, uint32(5))

	for i := uint32(6); len(primes) < 10001; i++ {
		if IsPrime(primes, i) {
			primes = append(primes, i)
		}
	}

	fmt.Printf("10001st prime: %d\n", primes[len(primes) -1])
}
