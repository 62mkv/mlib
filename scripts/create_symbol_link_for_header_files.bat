cd /usr/include
sudo mkdir sys
cd sys
sudo ln -s /usr/include/x86_64-linux-gnu/sys/cdefs.h

cd /usr/include
sudo mkdir bits
cd bits
sudo ln -s /usr/include/x86_64-linux-gnu/bits/wordsize.h

#install the stubs-32.h file with below command:
sudo apt-get install g++-multilib


cd /usr/include
sudo mkdir gnu
sudo ln -s /usr/include/x86_64-linux-gnu/gnu/stubs.h
sudo ln -s /usr/include/x86_64-linux-gnu/gnu/stubs-32.h


#downgrade gcc version to 4.4:
sudo apt-get install gcc-4.4

sudo ln -f -s /usr/bin/gcc-ar-4.4 /usr/bin/gcc-ar
sudo ln -f -s /usr/bin/gcc-nm-4.4 /usr/bin/gcc-nm
sudo ln -f -s /usr/bin/gcc-ranlib-4.4 /usr/bin/gcc-ranlib
sudo ln -f -s /usr/bin/g++-4.4 /usr/bin/g++
