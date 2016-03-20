cd ../installer
make
sudo make install
ls -lR /opt/akashic-storage
cd -

whoami
export

DIR=/tmp/akashic-storage-test
mkdir -p $DIR

echo "**** start ****"
service akashic-storage start
sleep 10
service akashic-storage status

# hostname, port, passwd
akashic-admin-config <<INP


passwd
INP
cat ~/.akashic-admin

userId=`akashic-admin-add`
echo "userId: $userId"
echo -- daemon.log --
cat /var/akashic-storage/log/daemon.log
echo -- daemon.err --
cat /var/akashic-storage/log/daemon.err
echo -- error.log --
cat /var/akashic-storage/log/error.log
echo -- all.log --
cat /var/akashic-storage/log/all.log


# alias, hostname, port
akashic-admin-setup-mc $userId <<INP
aka

10946
INP
cat ~/.mc/config.json; echo

echo "akiradeveloper" > file-up
echo -- daemon.log --
cat /var/akashic-storage/log/daemon.log
echo -- daemon.err --
cat /var/akashic-storage/log/daemon.err
echo -- error.log --
cat /var/akashic-storage/log/error.log
echo -- all.log --
cat /var/akashic-storage/log/all.log

mc mb aka/myb; echo
mc cp file-up aka/myb/myo; echo
mc ls aka/myb; echo
tree $DIR

ls -lR /var/akashic-storage

echo -- daemon.log --
cat /var/akashic-storage/log/daemon.log
echo -- daemon.err --
cat /var/akashic-storage/log/daemon.err
echo -- error.log --
cat /var/akashic-storage/log/error.log
echo -- all.log --
cat /var/akashic-storage/log/all.log

echo "**** stop ****"
service akashic-storage stop
sleep 10
service akashic-storage status

echo "**** start ****"
service akashic-storage start
sleep 10
service akashic-storage status
userId=`akashic-admin-add`
echo "userId: $userId"
tree $DIR
cat ~/.mc/config.json; echo
mc ls aka; echo
mc ls aka/myb; echo
mc cat aka/myb/myo > file-down; echo
diff file-up file-down

echo -- error.log --
cat /var/akashic-storage/log/error.log
echo -- all.log --
cat /var/akashic-storage/log/all.log
