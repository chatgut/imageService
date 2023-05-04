FROM ubuntu:latest
LABEL authors="mikaeledwartz"

ENTRYPOINT ["top", "-b"]
