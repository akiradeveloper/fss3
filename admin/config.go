package main

import (
	"./lib"
	"bufio"
	"fmt"
	"os"
	"strconv"
	"strings"
)

func main() {
	reader := bufio.NewReader(os.Stdin)
	fmt.Print("hostname (default: localhost): ")
	hostName, _ := reader.ReadString('\n')
	hostName = strings.Trim(hostName, "\n")
	if hostName == "" {
		hostName = "localhost"
	}

	fmt.Print("port# (default: 10946): ")
	portNumberS, _ := reader.ReadString('\n')
	portNumberS = strings.Trim(portNumberS, "\n")
	if portNumberS == "" {
		portNumberS = "10946"
	}
	portNumber, _ := strconv.Atoi(portNumberS)

	fmt.Print("admin passwd: ")
	passwd, _ := reader.ReadString('\n')
	passwd = strings.Trim(passwd, "\n")

	config := lib.Config{hostName, portNumber, passwd}
	config.Debug()

	path := os.Getenv("HOME") + "/.akashic-admin"
	config.Encode(path)
}
