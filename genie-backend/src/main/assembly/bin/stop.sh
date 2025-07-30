#!/bin/sh

# stop.sh所在路径
SHDIR=$(cd "$(dirname "$0")"; pwd)
# 发布包路径
BASEDIR=$(cd $SHDIR/..; pwd)
cd $BASEDIR
[[  $APP_NAME ]] || {
    export APP_NAME="genie-backend"
}

# 应用jar包和conf文件所在路径 最终也要包含在应用进程里，是获取进程的依据
CLASSPATH="$BASEDIR/conf/:$BASEDIR/lib/*"

function get_pid
{
    pgrep -lf "java .* $CLASSPATH"
}

echo "Stopping $APP_NAME ...."

if ! get_pid; then
    echo "App not running, exit"
else
    pkill -9 -f "java .* $CLASSPATH"
    sleep 5
fi

if get_pid; then
    echo "Stop $APP_NAME failed..."
    exit 1
fi