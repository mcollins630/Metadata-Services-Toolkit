if [ $# -eq 0 ]
then
	echo 'usage examples: '
	echo '  ./scripts/go.sh 170 restart_tomcat'
	echo '  ./scripts/go.sh 170 remote backup'
	exit
fi
. scripts/env/${1}.sh
. scripts/env/${1}.pass.sh 2> /dev/null
export BACKUP_FOLDER="${REMOTE_BASE_DIR}/backups/$(date +%Y%m%d_%H%M%S)"
scripts/${2}.sh ${1} ${2} ${3}
. scripts/env/local.sh
. scripts/env/local.pass.sh
