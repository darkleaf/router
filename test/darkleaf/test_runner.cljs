(ns darkleaf.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [darkleaf.router-test]))

(doo-tests 'darkleaf.router-test)
