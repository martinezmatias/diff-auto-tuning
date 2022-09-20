for d in $1/*; do
    echo "$d"
    find $d -name "result_size_per_config*.zip" | wc -l
done
