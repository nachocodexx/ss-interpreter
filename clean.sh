docker-compose -f $(pwd)/target/output/$1 down && sudo rm -rf $(pwd)/target/sink && sudo rm -rf $(pwd)/target/logs
