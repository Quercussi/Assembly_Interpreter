#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[]) {

    // if (argc != 3) {
    //     printf("error: usage: %s <assembly-code-file> <machine-code-file>\n",
    //         argv[0]);
    //     exit(1);
    // }

    char args[256] = "";
    int i;
    for(i = 1; i < argc; i++)
        sprintf(args, "%s ..\\%s", args, argv[i]);

    char commandLine[318];
    if(sprintf(commandLine,"java -classpath out\\production\\Assembly_Interpreter Assembler %s", args) < 0)
        printf("error: there are failures in reading command line arguments.");

    i = system(commandLine);
    if(i != 0)
        exit(i);

    return 0;
}