package main

import "fmt"

func main() {
	n0 := 5
	n1 := 7
	result := 0

	for i := 0; i < 100; i++ {
		result = n0 * n1
	}

	fmt.Println(result)
}
