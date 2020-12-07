if [[ -d "instructions/cloud-hosted-draft-$1" ]]; then
    rm -rf instructions/cloud-hosted-draft-$1
    echo "Removed cloud-hosted-draft-$1"
else
    echo "No draft guide found (cloud-hosted-draft-$1)"
fi