import os

javahome="/Library/Java/JavaVirtualMachines/jdk-9.0.4.jdk/Contents/Home/bin/"
liblocation= "{}/libs/gpgt-0.0.1-SNAPSHOT-jar-with-dependencies.jar".format(os.path.dirname(os.path.abspath(__file__)))
GRID5K_TIME_OUT = "00:59:00"
outResult= "./out/"
megadiffpath="/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded/"