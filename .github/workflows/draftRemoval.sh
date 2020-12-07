if [[ -d "instructions/draft-cloud-hosted-$1" ]]; then
    rm -rf instructions/draft-cloud-hosted-$1
    echo "Removed draft-cloud-hosted-$1"
else
    echo "No draft guide found (draft-cloud-hosted-$1)"
fi