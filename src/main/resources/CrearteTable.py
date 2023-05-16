import os
granularity = "jdt"
path = "/Users/matias/develop/gt-tuning/git-code-gpgt/out/".format(granularity)


def getdata():
    for folder in os.listdir(path):

        if folder == ".DS_Store":
            continue

        params = folder.split("_")
        print(params[9])
        attempts = params[9]
        results = open(os.path.join(path, folder, ("eval_{}_summary_tpe_default_testing.csv".format(attempts))))

        lines = results.readlines()

        mean = lines[1]
        median = lines[2]
        std = lines[3]

        print(mean, median, std)
        print(params)


getdata()