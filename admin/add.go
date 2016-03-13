package main

import (
	"./lib"
	"log"
	"net/http"
)

func main() {
	config := lib.ReadConfig()
	url := lib.AdminURL(config.HostName, config.PortNumber)

	req, err := http.NewRequest("POST", url, lib.EmptyReader)
	req.SetBasicAuth("admin", config.Passwd)

	res, err := http.DefaultClient.Do(req)
	log.Println(res, err)
}
