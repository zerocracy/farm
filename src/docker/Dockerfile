FROM yegor256/rultor
MAINTAINER Kirill <g4s8.public@gmail.com>
Label Description="Zerocracy farm rultor image"
WORKDIR /tmp

# Ruby
RUN apt-get update && \
	apt-get install -y ruby-dev libmagic-dev zlib1g-dev && \
	gpg2 --keyserver hkp://keys.gnupg.net --recv-keys D39DC0E3 && \
	gpg2 --keyserver hkp://pool.sks-keyservers.net --recv-keys 409B6B1796C275462A1703113804BB82D39DC0E3 7D2BAF1CF37B13E2069D6956105BD0E739499BDB && \
	curl -L https://get.rvm.io | bash -s stable && \
	/bin/bash -l -c ". /etc/profile.d/rvm.sh && rvm install ruby-2.6.0 && rvm use 2.6.0" && \
    apt-get clean

# install ruby tools
RUN gem install pdd && \
    gem install xcop && \
    gem install sass -v 3.4.25

# install mongodb
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 0C49F3730359A14518585931BC711F9BA15703C6 && \
    echo "deb [ arch=amd64 ] http://repo.mongodb.org/apt/ubuntu trusty/mongodb-org/3.4 multiverse" | \
    tee /etc/apt/sources.list.d/mongodb-org-3.4.list && \
    apt-get clean

# remove invalid settings.xml
RUN rm -f /root/.m2/settings.xml

# warm up caches with farm deps
RUN git clone --depth=1 https://github.com/zerocracy/farm.git && \
	cd farm && \
	mvn dependency:go-offline && \
	cd .. && \
	rm -fr farm
