release:
	mkdir -p releases
	mvn -DaltDeploymentRepository=repo::default::file:releases clean deploy

snapshot-release:
	mkdir -p snapshots
	mvn -DaltDeploymentRepository=snapshot-repo::default::file:snapshots clean deploy

.PHONY: release snapshot-release
