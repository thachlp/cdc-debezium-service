#!/bin/bash

# Check if Docker service is running
if systemctl is-active --quiet docker
then
    echo "Docker is running."
else
    if command -v docker >/dev/null 2>&1; then
        echo "Docker is installed."
        echo "Docker version: $(docker --version)"
        sudo systemctl start docker
    else
        echo "Install Docker ..."
        sudo yum install -y yum-utils
        sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
        sudo yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
        sudo systemctl start docker
        sudo /usr/sbin/groupadd docker
        sudo /usr/sbin/usermod -aG docker irteam

cat > /etc/docker/daemon.json <<EOF
    {
        "log-driver": "local",
        "log-opts": {
            "max-size": "10m",
            "max-file": "5"
        }
    }
EOF

    sudo systemctl daemon-reload
    sudo systemctl restart docker
    docker info --format '{{.LoggingDriver}}'
    fi
fi

sudo systemctl enable docker
