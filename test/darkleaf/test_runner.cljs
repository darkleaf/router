(ns darkleaf.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [darkleaf.router-test]
            [darkleaf.router.util-test]))

(doo-tests 'darkleaf.router-test
           'darkleaf.router.util-test)
