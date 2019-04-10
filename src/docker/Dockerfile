FROM yegor256/rultor:latest
MAINTAINER Kirill <g4s8.public@gmail.com>
Label Description="Zerocracy farm rultor image"

# install ruby tools
RUN bash -l -c "gem install pdd" && \
    bash -l -c "gem install xcop" && \
    bash -l -c "gem install sass -v 3.4.25"

# install mongodb
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 0C49F3730359A14518585931BC711F9BA15703C6 && \
    echo "deb [ arch=amd64 ] http://repo.mongodb.org/apt/ubuntu trusty/mongodb-org/3.4 multiverse" | \
    tee /etc/apt/sources.list.d/mongodb-org-3.4.list && \
    apt-get clean

# remove invalid settings.xml
RUN rm -f /root/.m2/settings.xml
COPY settings.xml /root/.m2/settings.xml

# warm up caches with farm deps
RUN git clone --depth=1 https://github.com/zerocracy/farm.git && \
	cd farm && \
	mvn dependency:go-offline && \
	cd .. && \
	rm -fr farm
