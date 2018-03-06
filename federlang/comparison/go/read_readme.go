package main

import (
	"fmt"
	"io/ioutil"
)

func main() {
	buffer, err := ioutil.ReadFile("jfederc")
	if err != nil {
		panic(err)
	}

	fmt.Printf(string(buffer))
}
