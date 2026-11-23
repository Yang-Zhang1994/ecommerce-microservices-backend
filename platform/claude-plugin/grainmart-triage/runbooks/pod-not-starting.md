# Runbook: a pod is not starting (waiting state)

Check `list_pods` for the waiting reason, then:

| Waiting reason | Usual cause | Fix |
|---|---|---|
| `ErrImageNeverPull` (kind) / `ImagePullBackOff` (EKS) | The image tag in the release does not exist on the node or in ECR, often a typo or a build that never ran | Rebuild and load the image (`gm sandbox deploy NAME`), or roll back to the last good revision (`helm rollback`) |
| `CreateContainerConfigError` | The pod references a Secret or ConfigMap (or a key in one) that does not exist in the namespace | Create the missing Secret/key, or remove the reference; `describe_pod` names it |
| `ContainerCreating` for minutes | A volume or Secret volume cannot mount | `describe_pod` events show which |
