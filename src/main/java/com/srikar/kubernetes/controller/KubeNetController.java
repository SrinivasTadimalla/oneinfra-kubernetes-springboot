package com.srikar.kubernetes.controller;

import com.srikar.kubernetes.dto.IngressSummary;
import com.srikar.kubernetes.dto.ServiceSummary;
import com.srikar.kubernetes.service.KubeNetService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Network resources: Services & Ingress. */
@RestController
@RequestMapping("/k8s")
public class KubeNetController {

    private final KubeNetService net;

    public KubeNetController(KubeNetService net) {
        this.net = net;
    }

    /** List Services in a namespace (ports include targetPort/NodePort). */
    @GetMapping(value = "/services/{namespace}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ServiceSummary>> services(@PathVariable String namespace) {
        return ResponseEntity.ok(net.listServices(namespace));
    }

    /** List Ingress objects in a namespace. */
    @GetMapping(value = "/ingress/{namespace}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<IngressSummary>> ingress(@PathVariable String namespace) {
        return ResponseEntity.ok(net.listIngress(namespace));
    }
}
