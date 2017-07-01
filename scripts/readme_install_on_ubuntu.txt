1. comment out below line for file '$MLIBDIR/makefiles/Config.mk', this file will be auto generated when do 'make config'.
#LDOPTS     = -melf_i386 //to avoid gcc unknow command option error.

2. use JDK 32 instead of 64 to avoid below error:
...mlib/lib/libMOCA.so: 错误 ELF 类: ELFCLASS32 (Possible cause: architecture word width mismatch)

3. for 82.registry file.
need to use ':' instead of ';' for configing prod-dirs and CLASSPATH

4. put below into your ~/.bashrc
export MLIBDIR=/media/samni/96BA0DD5BA0DB333/MFC/mlib
export LESDIR=/media/samni/96BA0DD5BA0DB333/MFC/mlib
export MOCADIR=/media/samni/96BA0DD5BA0DB333/MFC/mlib
export MOCA_REGISTRY=/media/samni/96BA0DD5BA0DB333/MFC/mlib/src/resource/82.registry
export M2_HOME=~/mvn350
export JAVA_HOME=~/jdk180
export CLASSPATH=.:$JAVA_HOME/lib:$JAVA_HOME/jre/lib:$MLIBDIR/target/mlib.jar:$MLIBDIR/3rdparty/jetty/*:$MLIBDIR/3rdparty/servlet/*:$MLIBDIR/3rdparty/spring/*:$MLIBDIR/3rdparty/joda/*:$MLIBDIR/3rdparty/el4j/*:$MLIBDIR/3rdparty/log4j/*:$MLIBDIR/3rdparty/hibernate/lib/*:$MLIBDIR/3rdparty/mad/*:$MLIBDIR/3rdparty/jboss/*:$MLIBDIR/3rdparty/groovy/*:$MLIBDIR/3rdparty/netty/*:$MLIBDIR/3rdparty/json/*:$MLIBDIR/3rdparty/quartz/*:$MLIBDIR/3rdparty/google-collections/*:$CLASSPATH
export PATH=.:$JAVA_HOME/bin:$M2_HOME/bin:$MLIBDIR/bin:$PATH
export LD_LIBRARY_PATH=.:$MLIBDIR/lib:$LD_LIBRARY_PATH

5. Below is git alias:
alias c="clear"
alias sl="git log --oneline "
alias lb="git branch"
alias lba="git branch -a"
alias lrg="git ls-remote | grep -i "
alias slg="git log --oneline --graph"
alias cm="git commit -m"
alias s="git show"
alias sp="git show --word-diff=porcelain"
alias f="git fetch"
alias p="git pull"
alias df="git diff"
alias dfc="git diff --cached"
alias au="git add -u"
alias co="git checkout"
alias gp="git grep -n -i "
alias rss="git reset --soft"
alias rsm="git reset --mixed"
alias rsh="git reset --hard"
alias ss="git status"
alias au="git add -u"
alias mg="git merge"
alias cld="git clean -fd"
alias clx="git clean -fx"
alias cla="git clean -fxd"
alias blm="git blame"
alias bm="git branch -M"
alias po="git push origin"
alias db="git branch -D"
alias del="git rm "
alias dlc="git rm --cached "
alias lf="git ls-files | grep -i "
#fix patch: remove tabs and tailing space.
alias fp="perl  -i.bak -pe \"s/(if|for|while)\(/\1 \(/ if/^\+/;
                             s/(?<! )(?<!<)(?<!>)(?<!-)(?<!=)(?<!!)(==|<|>)/ \1/g if/^\+\s*(else)?\s*if/;
                             s/(==|<|>)(?! )(?<!<)(?<!>)(?!-)(?!=)(?!!)/\1 /g if/^\+\s*(else)?\s*if/;
                             s/\)\{/\) \{/g if/^\+/;
#apply patch with fixing tailing space.
alias ap="git apply --recount --verbose --whitespace=fix"
#Reverse patch.
alias apr="git apply -R  --whitespace=fix"
alias gpb="git branch -a | grep -i "
alias chp="git cherry-pick "
alias ssth="git stash save "
alias lsth="git stash list "
alias psth="git stash pop "

