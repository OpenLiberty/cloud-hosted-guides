if [[ -d "instructions/draft-$1" ]]; then
    rm -rf instructions/draft-$1
    echo "Removed draft-$1"
else
    echo "No draft guide found (draft-$1)"
fi